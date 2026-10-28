const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');

test('Firestore rules syntax and structure validation', () => {
  const rules = fs.readFileSync('firestore.rules', 'utf8');
  assert.ok(rules.includes("rules_version = '2'"), 'Must declare rules_version 2');
  assert.ok(rules.includes("service cloud.firestore"), 'Must declare cloud.firestore service');
  assert.ok(rules.includes("isAuthenticated()"), 'Must define isAuthenticated helper');
  assert.ok(rules.includes("match /users/{userId}"), 'Must define users collection');
  assert.ok(rules.includes("match /listings/{listingId}"), 'Must define listings collection');
});
