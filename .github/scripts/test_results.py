#!/usr/bin/env python3
"""Resume los resultados de surefire en lineas ordenadas 'clase#test<TAB>ESTADO'.

Uso: test_results.py <dir_reportes> <archivo_salida>
Sirve para comparar corridas sucesivas (criterio: 0 tests flaky, 3 corridas identicas).
"""
import glob
import sys
import xml.etree.ElementTree as ET


def main(reports_dir: str, out_file: str) -> int:
    lines = []
    for path in glob.glob(f"{reports_dir}/TEST-*.xml"):
        for case in ET.parse(path).getroot().iter("testcase"):
            if case.find("failure") is not None or case.find("error") is not None:
                status = "FAIL"
            elif case.find("skipped") is not None:
                status = "SKIP"
            else:
                status = "PASS"
            lines.append(f"{case.get('classname')}#{case.get('name')}\t{status}")
    lines.sort()
    with open(out_file, "w", encoding="utf-8") as fh:
        fh.write("\n".join(lines) + "\n")
    total = len(lines)
    bad = sum(1 for l in lines if l.endswith("FAIL"))
    skipped = sum(1 for l in lines if l.endswith("SKIP"))
    print(f"{out_file}: total={total} fallados={bad} salteados={skipped}")
    return 0 if total > 0 else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1], sys.argv[2]))
