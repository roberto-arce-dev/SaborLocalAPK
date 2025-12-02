#!/usr/bin/env python3
"""
Fix broken KDoc comments in Kotlin files.

This script finds lines starting with "     * " (5 spaces + asterisk) that are
not inside a proper KDoc block and wraps them with /** and */.
"""

import re
import sys
from pathlib import Path


def fix_kdoc_in_file(filepath):
    """Fix KDoc comments in a single file."""
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    fixed_lines = []
    i = 0
    while i < len(lines):
        line = lines[i]

        # Check if this is a broken KDoc line (starts with "     * " but not "    /**")
        if line.startswith('     * ') and not line.startswith('    /**'):
            # Find the end of the comment block
            comment_lines = []
            j = i
            while j < len(lines) and lines[j].startswith('     * '):
                comment_lines.append(lines[j])
                j += 1

            # Add opening /**
            fixed_lines.append('    /**\n')
            # Add the comment content
            fixed_lines.extend(comment_lines)
            # Add closing */
            fixed_lines.append('     */\n')

            # Skip past the comment block
            i = j
        else:
            fixed_lines.append(line)
            i += 1

    # Write back
    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(fixed_lines)

    return len([l for l in lines if l.startswith('     * ') and not l.startswith('    /**')])


def main():
    viewmodel_dir = Path('app/src/main/java/com/example/miappmodular/viewmodel')

    if not viewmodel_dir.exists():
        print(f"Error: Directory {viewmodel_dir} not found")
        sys.exit(1)

    kotlin_files = list(viewmodel_dir.glob('*.kt'))

    if not kotlin_files:
        print(f"No Kotlin files found in {viewmodel_dir}")
        sys.exit(1)

    total_fixed = 0
    for filepath in sorted(kotlin_files):
        fixed_count = fix_kdoc_in_file(filepath)
        if fixed_count > 0:
            print(f"✅ Fixed {fixed_count} broken KDoc comments in {filepath.name}")
            total_fixed += fixed_count
        else:
            print(f"⏭️  No issues in {filepath.name}")

    print(f"\n✅ Total: Fixed {total_fixed} broken KDoc comments in {len(kotlin_files)} files")


if __name__ == '__main__':
    main()
