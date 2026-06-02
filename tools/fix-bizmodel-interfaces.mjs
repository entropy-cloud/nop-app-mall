import { readdir, readFile, writeFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const serviceDir = path.join(repoRoot, 'app-mall-service', 'src', 'main', 'java', 'app', 'mall', 'service', 'entity')
const bizDir = path.join(repoRoot, 'app-mall-dao', 'src', 'main', 'java', 'app', 'mall', 'biz')

async function listJavaFiles(dir) {
  const entries = await readdir(dir, { withFileTypes: true })
  return entries
    .filter((entry) => entry.isFile() && entry.name.endsWith('.java'))
    .map((entry) => path.join(dir, entry.name))
}

function ensureImport(source, importLine) {
  if (source.includes(importLine)) {
    return source
  }

  const lines = source.split(/\r?\n/)
  const packageIndex = lines.findIndex((line) => line.startsWith('package '))
  let insertIndex = packageIndex >= 0 ? packageIndex + 1 : 0

  while (insertIndex < lines.length && lines[insertIndex].trim() === '') {
    insertIndex += 1
  }

  while (insertIndex < lines.length && lines[insertIndex].startsWith('import ')) {
    insertIndex += 1
  }

  lines.splice(insertIndex, 0, importLine)
  return lines.join('\n')
}

function ensureImplements(source, entityName, interfaceName) {
  const classPattern = new RegExp(
    `public class\\s+${entityName}BizModel\\s+extends\\s+CrudBizModel<${entityName}>\\s*(\\{)`
  )

  if (new RegExp(`public class\\s+${entityName}BizModel\\b[^\\n]*\\bimplements\\b`).test(source)) {
    return source
  }

  return source.replace(
    classPattern,
    `public class ${entityName}BizModel extends CrudBizModel<${entityName}> implements ${interfaceName} $1`
  )
}

async function main() {
  const bizFiles = await listJavaFiles(bizDir)
  const bizInterfaces = new Map(
    bizFiles.map((file) => {
      const name = path.basename(file, '.java')
      return [name, file]
    })
  )

  const serviceFiles = await listJavaFiles(serviceDir)
  const updated = []

  for (const file of serviceFiles) {
    const fileName = path.basename(file, '.java')
    if (!fileName.endsWith('BizModel')) {
      continue
    }

    const entityName = fileName.slice(0, -'BizModel'.length)
    const interfaceName = `I${entityName}Biz`
    if (!bizInterfaces.has(interfaceName)) {
      continue
    }

    const original = await readFile(file, 'utf8')
    if (original.includes(`implements ${interfaceName}`)) {
      continue
    }

    let next = original
    next = ensureImport(next, `import app.mall.biz.${interfaceName};`)
    next = ensureImplements(next, entityName, interfaceName)

    if (next !== original) {
      await writeFile(file, next, 'utf8')
      updated.push(path.relative(repoRoot, file))
    }
  }

  if (updated.length === 0) {
    console.log('No BizModel files required updates.')
    return
  }

  console.log(`Updated ${updated.length} BizModel files:`)
  for (const file of updated) {
    console.log(`- ${file}`)
  }
}

await main()
