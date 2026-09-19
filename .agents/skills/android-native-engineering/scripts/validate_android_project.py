#!/usr/bin/env python3
"""Valida invariantes estruturais mínimos de um projeto Android sem modificá-lo."""

from __future__ import annotations

import argparse
from pathlib import Path
import sys


def validate(root: Path) -> list[str]:
    violations: list[str] = []
    if not root.is_dir():
        return [f"projeto não encontrado: {root}"]

    wrappers = (root / "gradlew", root / "gradlew.bat")
    if not any(path.is_file() for path in wrappers):
        violations.append("Gradle Wrapper ausente (gradlew ou gradlew.bat)")

    if not any((root / name).is_file() for name in ("settings.gradle.kts", "settings.gradle")):
        violations.append("settings.gradle(.kts) ausente")

    if not list(root.glob("**/src/main/AndroidManifest.xml")):
        violations.append("nenhum src/main/AndroidManifest.xml encontrado")

    build_files = list(root.glob("**/build.gradle.kts")) + list(root.glob("**/build.gradle"))
    if not build_files:
        violations.append("nenhum build.gradle(.kts) encontrado")

    if (root / "local.properties").is_file():
        violations.append("arquivo local potencialmente sensível presente: local.properties")

    return violations


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("project", type=Path, help="raiz do projeto Android")
    args = parser.parse_args()
    violations = validate(args.project.resolve())
    if violations:
        for violation in violations:
            print(f"ERRO: {violation}")
        return 1
    print("OK: invariantes estruturais Android atendidos")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
