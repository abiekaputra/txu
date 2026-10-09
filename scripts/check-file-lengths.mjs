import { readdir, readFile } from 'node:fs/promises';
import { extname, join, relative } from 'node:path';

const root = process.cwd();
const ignored = new Set([
  '.git',
  '.gradle',
  'build',
  'dist',
  'node_modules',
  'playwright-report',
  'test-results',
]);
const productionExtensions = new Set([
  '.css',
  '.html',
  '.js',
  '.jsx',
  '.kt',
  '.kts',
  '.mjs',
  '.ts',
  '.tsx',
]);
const violations = [];

async function inspect(directory) {
  for (const entry of await readdir(directory, { withFileTypes: true })) {
    if (ignored.has(entry.name)) continue;
    const path = join(directory, entry.name);
    if (entry.isDirectory()) {
      await inspect(path);
      continue;
    }
    if (!productionExtensions.has(extname(entry.name))) continue;
    const lines = (await readFile(path, 'utf8')).split('\n').length;
    const testFile =
      /(?:test|spec|Test)\./.test(entry.name) || path.includes('/test/');
    const limit = testFile ? 1000 : 300;
    if (lines > limit)
      violations.push(`${relative(root, path)}: ${lines}/${limit}`);
  }
}

await inspect(root);
if (violations.length) {
  console.error(`File length limits failed:\n${violations.join('\n')}`);
  process.exit(1);
}
console.log('File length limits passed.');
