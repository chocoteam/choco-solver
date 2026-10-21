#!/usr/bin/env python3
# Lists the failed tests of all modules, from the TestNG reports (*/target/surefire-reports/testng-results.xml).
# Used by the CI, where Maven runs with -Dmaven.test.failure.ignore=true so that every module is tested:
# prints the failures, adds them to the job summary, and exits with 1 if there is at least one.

import glob
import os
import sys
import xml.etree.ElementTree as ET

reports = sorted(glob.glob("**/target/surefire-reports/testng-results.xml", recursive=True))
if not reports:
    print("::warning::No TestNG report found")

failures = []
total = 0
for report in reports:
    root = ET.parse(report).getroot()
    total += int(root.get("total", 0))
    for clazz in root.iter("class"):
        for method in clazz.iter("test-method"):
            if method.get("status") != "FAIL":
                continue
            name = clazz.get("name") + "." + method.get("name")
            if method.get("is-config") == "true":
                name += " (configuration)"
            exception = method.find("exception")
            lines = method.findtext("exception/message", default="").strip().splitlines()
            message = lines[0] if lines else (exception.get("class", "") if exception is not None else "")
            failures.append((name, message))

print(f"{len(failures)} failed test(s) out of {total}")
for name, message in failures:
    print(f"::error title={name}::{message}")

summary = os.environ.get("GITHUB_STEP_SUMMARY")
if summary:
    with open(summary, "a") as out:
        out.write(f"### {len(failures)} failed test(s) out of {total}\n\n")
        for name, message in failures:
            out.write(f"- `{name}`: {message}\n")

sys.exit(1 if failures else 0)
