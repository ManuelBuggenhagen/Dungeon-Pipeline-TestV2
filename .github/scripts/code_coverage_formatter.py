#!/usr/bin/env python3

import argparse
import glob
import os
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


DEFAULT_PATTERN = "**/build/JacocoReports/test/jacocoTestReport.xml"

METRIC_ORDER = [
    "LINE",
    "BRANCH",
    "METHOD",
    "CLASS",
    "INSTRUCTION",
    "COMPLEXITY",
]

# Metrik -> Env-Variable fuer die optionale Mindest-Coverage (analog DesigniteJava).
# Grenzwerte extern ueber Repository Variables setzen.
# Ist eine Variable nicht gesetzt (bzw. -1), wird die Metrik nicht geprueft.
METRIC_ENV = {
    "LINE": "MIN_LINE",
    "BRANCH": "MIN_BRANCH",
    "METHOD": "MIN_METHOD",
    "CLASS": "MIN_CLASS",
    "INSTRUCTION": "MIN_INSTRUCTION",
    "COMPLEXITY": "MIN_COMPLEXITY",
}


def parse_args():
    parser = argparse.ArgumentParser(
        description="Format JaCoCo XML reports as a GitHub Actions summary."
    )

    parser.add_argument(
        "--pattern",
        default=DEFAULT_PATTERN,
        help=f"Glob pattern for JaCoCo XML reports. Default: {DEFAULT_PATTERN}",
    )

    parser.add_argument(
        "--summary-file",
        default=os.environ.get("GITHUB_STEP_SUMMARY"),
        help="GitHub step summary file. Defaults to GITHUB_STEP_SUMMARY.",
    )

    parser.add_argument(
        "--min-line-coverage",
        type=float,
        default=None,
        help="Optional minimum line coverage percentage. Fails if below this value.",
    )

    parser.add_argument(
        "--warning-line-coverage",
        type=float,
        default=60.0,
        help="Line coverage below this value is shown as critical.",
    )

    parser.add_argument(
        "--good-line-coverage",
        type=float,
        default=80.0,
        help="Line coverage at or above this value is shown as good.",
    )

    parser.add_argument(
        "--branch-name",
        default=os.environ.get("GITHUB_HEAD_REF") or os.environ.get("GITHUB_REF_NAME"),
        help="Branch name shown in the coverage summary."
    )

    parser.add_argument(
        "--base-branch",
        default=os.environ.get("GITHUB_BASE_REF"),
        help="BASE branch for pull request."
    )

    parser.add_argument(
        "--commit-sha",
        default=os.environ.get("GITHUB_SHA"),
        help="Commit SHA shown in the coverage summary."
    )

    parser.add_argument(
        "--path",
        default=os.environ.get("COVERAGE_PATH", ""),
        help=(
            "Optionaler Quellpfad-/Paket-Prefix (z.B. 'de/dungeon/core' oder "
            "'de.dungeon.core'). Ist er gesetzt, wird zusaetzlich die Coverage nur "
            "fuer diesen Pfad ausgegeben. Leer/ungesetzt = keine Zusatzauswertung. "
            "Default kommt aus der Env-Variable COVERAGE_PATH."
        ),
    )

    return parser.parse_args()


def float_env(name):
    """Liest eine Env-Grenze; ungesetzt/leer/ungueltig -> -1 (= nicht pruefen)."""
    raw = os.environ.get(name, "").strip()

    if raw == "":
        return -1.0

    try:
        return float(raw)
    except ValueError:
        return -1.0


def evaluate_gates(totals):
    """Wertet das lokale Quality Gate aus.

    Liefert (gates, failed):
      gates  -> Liste (Metrik, Coverage, Minimum) fuer jede gesetzte Grenze.
      failed -> Teilmenge, deren Coverage das Minimum unterschreitet (oder fehlt).
    """
    gates = []

    for metric in METRIC_ORDER:
        minimum = float_env(METRIC_ENV[metric])

        if minimum < 0:
            continue

        if metric in totals:
            coverage = percentage(
                totals[metric]["covered"],
                totals[metric]["missed"],
            )
        else:
            coverage = None

        gates.append((metric, coverage, minimum))

    failed = [
        (metric, coverage, minimum)
        for (metric, coverage, minimum) in gates
        if coverage is None or coverage < minimum
    ]

    return gates, failed


def collect_reports(pattern):
    return sorted(glob.glob(pattern, recursive=True))


def module_name_from_report_path(report_file):
    path = Path(report_file)
    parts = path.parts

    if "build" in parts:
        build_index = parts.index("build")

        if build_index == 0:
            return "root"

        return parts[build_index - 1]

    return str(path.parent)


def counters_from_element(element):
    counters = {}

    for counter in element.findall("counter"):
        counter_type = counter.attrib["type"]
        counters[counter_type] = {
            "missed": int(counter.attrib["missed"]),
            "covered": int(counter.attrib["covered"]),
        }

    return counters


def parse_report(report_file):
    tree = ET.parse(report_file)
    root = tree.getroot()

    counters = counters_from_element(root)

    packages = [
        (package.attrib.get("name", ""), counters_from_element(package))
        for package in root.findall("package")
    ]

    return counters, packages


def normalize_path(raw):
    """Normalisiert den Pfad-Filter: Punkte -> Slashes, umschliessende Slashes weg."""
    return raw.strip().replace(".", "/").strip("/")


def package_matches(package_name, path_filter):
    """True, wenn der Paketname exakt dem Filter entspricht oder darunter liegt."""
    name = package_name.strip("/")

    return name == path_filter or name.startswith(path_filter + "/")

#test
def direct_child_key(package_name, path_filter):
    """Ordnet ein (passendes) Package seinem direkten Unterpfad unter path_filter zu.

    Beispiele fuer path_filter='contrib':
      'contrib'               -> 'contrib' (Klassen direkt im Pfad)
      'contrib/hud'           -> 'contrib/hud'
      'contrib/hud/inventory' -> 'contrib/hud'
    """
    name = package_name.strip("/")

    if name == path_filter:
        return path_filter

    remainder = name[len(path_filter) + 1:]
    first_segment = remainder.split("/", 1)[0]

    return f"{path_filter}/{first_segment}"


def add_counter(totals, counter_type, missed, covered):
    if counter_type not in totals:
        totals[counter_type] = {
            "missed": 0,
            "covered": 0,
        }

    totals[counter_type]["missed"] += missed
    totals[counter_type]["covered"] += covered


def percentage(covered, missed):
    total = covered + missed

    if total == 0:
        return None

    return covered / total * 100


def format_percentage(value):
    if value is None:
        return "n/a"

    return f"{value:.2f}%"


def coverage_bar(value, width=20):
    if value is None:
        return "n/a"

    filled = round((value / 100) * width)
    empty = width - filled

    return "█" * filled + "░" * empty


def status_for_coverage(value, warning_threshold, good_threshold):
    if value is None:
        return "⚪ Unbekannt"

    if value >= good_threshold:
        return "🟢 Gut"

    if value >= warning_threshold:
        return "🟡 Ausbaufähig"

    return "🔴 Kritisch"


def metric_label(metric):
    labels = {
        "LINE": "Lines",
        "BRANCH": "Branches",
        "METHOD": "Methods",
        "CLASS": "Classes",
        "INSTRUCTION": "Instructions",
        "COMPLEXITY": "Complexity",
    }

    return labels.get(metric, metric.title())



def build_branch_section(branch_name, base_branch, commit_sha):
    lines = []

    lines.append("> [!IMPORTANT]")
    lines.append(f"> **Ergebnisse für Branch:** `{branch_name or 'unbekannt'}`")

    if base_branch:
        lines.append(f"> **Ziel-Branch:** `{base_branch}`")

    if commit_sha:
        lines.append(f"> **Commit:** `{commit_sha[:7]}`")

    lines.append("")
    return lines


def build_total_table(totals):
    lines = []

    lines.append("## Gesamtübersicht")
    lines.append("")
    lines.append("| Metrik | Coverage | Balken | Covered | Missed |")
    lines.append("|---|---:|---|---:|---:|")

    for metric in METRIC_ORDER:
        if metric not in totals:
            continue

        covered = totals[metric]["covered"]
        missed = totals[metric]["missed"]
        coverage = percentage(covered, missed)

        lines.append(
            f"| {metric_label(metric)} "
            f"| **{format_percentage(coverage)}** "
            f"| `{coverage_bar(coverage)}` "
            f"| {covered} "
            f"| {missed} |"
        )

    return lines


def build_path_table(path_filter, path_totals, path_children, matched):
    lines = []

    lines.append(f"## Coverage für Pfad `{path_filter}`")
    lines.append("")

    if not matched:
        lines.append("> [!WARNING]")
        lines.append(
            f"> Für den Pfad `{path_filter}` wurden keine passenden Pakete "
            "in den Reports gefunden. Ist der Pfad korrekt gesetzt?"
        )

        return lines

    # Eine Tabelle: oben der gesamte Pfad, darunter die direkten Unterpfade.
    lines.append("| Paket | Line Coverage | Branch Coverage | Method Coverage | Class Coverage |")
    lines.append("|---|---:|---:|---:|---:|")

    # Ober-Package (Aggregat ueber den gesamten Pfad, inkl. aller Unterordner).
    lines.append(
        coverage_row(f"**`{path_filter}` (gesamt)**", path_totals, emphasis=True)
    )

    # Trennung zwischen Ober- und Unterpaketen.
    if path_children:
        lines.append("| **↳ direkte Unterpfade** | | | | |")

        for child_name in sorted(path_children):
            lines.append(coverage_row(f"`{child_name}`", path_children[child_name]))

    return lines


def build_gate_table(gates):
    lines = []

    lines.append("## 🚦 Quality Gate")
    lines.append("")
    lines.append("| Metrik | Coverage | Minimum | Status |")
    lines.append("|---|---:|---:|---|")

    for metric, coverage, minimum in gates:
        passed = coverage is not None and coverage >= minimum
        status = "✅ Pass" if passed else "❌ Fail"

        lines.append(
            f"| {metric_label(metric)} "
            f"| {format_percentage(coverage)} "
            f"| {minimum:.2f}% "
            f"| {status} |"
        )

    return lines


def metric_cells(counters):
    """Formatierte Line/Branch/Method/Class-Coverage als Liste von Zellen."""
    cells = []

    for metric in ("LINE", "BRANCH", "METHOD", "CLASS"):
        cells.append(format_percentage(percentage(
            counters.get(metric, {}).get("covered", 0),
            counters.get(metric, {}).get("missed", 0),
        )))

    return cells


def coverage_row(label, counters, emphasis=False):
    """Eine Tabellenzeile mit Line/Branch/Method/Class-Coverage.

    label wird woertlich uebernommen (inkl. eigener Formatierung). Bei emphasis
    werden Label und Werte fett dargestellt.
    """
    cells = metric_cells(counters)

    if emphasis:
        cells = [f"**{cell}**" for cell in cells]

    return "| " + label + " | " + " | ".join(cells) + " |"


def build_module_table(module_results):
    lines = []

    lines.append("## Coverage pro Modul")
    lines.append("")
    lines.append("| Modul | Line Coverage | Branch Coverage | Method Coverage | Class Coverage |")
    lines.append("|---|---:|---:|---:|---:|")

    for module_name, counters in module_results:
        line = percentage(
            counters.get("LINE", {}).get("covered", 0),
            counters.get("LINE", {}).get("missed", 0),
        )
        branch = percentage(
            counters.get("BRANCH", {}).get("covered", 0),
            counters.get("BRANCH", {}).get("missed", 0),
        )
        method = percentage(
            counters.get("METHOD", {}).get("covered", 0),
            counters.get("METHOD", {}).get("missed", 0),
        )
        clazz = percentage(
            counters.get("CLASS", {}).get("covered", 0),
            counters.get("CLASS", {}).get("missed", 0),
        )

        lines.append(
            f"| `{module_name}` "
            f"| {format_percentage(line)} "
            f"| {format_percentage(branch)} "
            f"| {format_percentage(method)} "
            f"| {format_percentage(clazz)} |"
        )

    return lines


def build_report_paths_section(report_files):
    lines = []

    lines.append("<details>")
    lines.append("<summary>Gefundene JaCoCo-Reports anzeigen</summary>")
    lines.append("")
    lines.append("")

    for report_file in report_files:
        lines.append(f"- `{report_file}`")

    lines.append("")
    lines.append("</details>")

    return lines


def build_summary(
        report_files,
        totals,
        module_results,
        warning_threshold,
        good_threshold,
        branch_name=None,
        base_branch=None,
        commit_sha=None,
        gates=None,
        path_filter="",
        path_totals=None,
        path_children=None,
        path_matched=False
        ):
    line_coverage = None

    if "LINE" in totals:
        line_coverage = percentage(
            totals["LINE"]["covered"],
            totals["LINE"]["missed"],
        )

    status = status_for_coverage(
        line_coverage,
        warning_threshold,
        good_threshold,
    )

    lines = []

    lines.append("# JaCoCo Code Coverage")
    lines.append("")

    lines.extend(
        build_branch_section(
            branch_name=branch_name,
            base_branch=base_branch,
            commit_sha=commit_sha
        )
    )

    lines.append(f"**Status:** {status}")
    lines.append("")
    lines.append(f"**Line Coverage:** `{format_percentage(line_coverage)}`")
    lines.append("")
    lines.append(f"`{coverage_bar(line_coverage, width=30)}`")
    lines.append("")

    if line_coverage is not None and line_coverage < warning_threshold:
        lines.append("> [!WARNING]")
        lines.append(
            f"> Die Line Coverage liegt bei **{format_percentage(line_coverage)}** "
            f"und damit unter dem Warnwert von **{warning_threshold:.2f}%**."
        )
        lines.append("")
    elif line_coverage is not None and line_coverage >= good_threshold:
        lines.append("> [!TIP]")
        lines.append(
            f"> Die Line Coverage liegt bei **{format_percentage(line_coverage)}** "
            f"und erfüllt den Zielwert von **{good_threshold:.2f}%**."
        )
        lines.append("")
    else:
        lines.append("> [!NOTE]")
        lines.append(
            "> Die Coverage ist sichtbar, aber noch ausbaufähig. "
            "Für Details den HTML-Report aus den Artefakten herunterladen."
        )
        lines.append("")

    if gates:
        lines.extend(build_gate_table(gates))
        lines.append("")

    lines.extend(build_total_table(totals))
    lines.append("")
    lines.extend(build_module_table(module_results))
    lines.append("")

    if path_filter:
        lines.extend(build_path_table(path_filter, path_totals or {}, path_children or {}, path_matched))
        lines.append("")

    lines.extend(build_report_paths_section(report_files))
    lines.append("")

    return "\n".join(lines)


def main():
    args = parse_args()

    report_files = collect_reports(args.pattern)

    if not report_files:
        print(f"No JaCoCo XML reports found for pattern: {args.pattern}", file=sys.stderr)
        return 1

    path_filter = normalize_path(args.path)

    totals = {}
    module_results = []
    path_totals = {}
    path_children = {}
    path_matched = False

    for report_file in report_files:
        counters, packages = parse_report(report_file)
        module_name = module_name_from_report_path(report_file)

        module_results.append((module_name, counters))

        for counter_type, values in counters.items():
            add_counter(
                totals,
                counter_type,
                values["missed"],
                values["covered"],
            )

        if path_filter:
            for package_name, package_counters in packages:
                if not package_matches(package_name, path_filter):
                    continue

                path_matched = True
                child_totals = path_children.setdefault(
                    direct_child_key(package_name, path_filter), {}
                )

                for counter_type, values in package_counters.items():
                    add_counter(
                        path_totals,
                        counter_type,
                        values["missed"],
                        values["covered"],
                    )
                    add_counter(
                        child_totals,
                        counter_type,
                        values["missed"],
                        values["covered"],
                    )

    gates, failed = evaluate_gates(totals)

    summary = build_summary(
        report_files=report_files,
        totals=totals,
        module_results=module_results,
        warning_threshold=args.warning_line_coverage,
        good_threshold=args.good_line_coverage,
        branch_name=args.branch_name,
        base_branch=args.base_branch,
        commit_sha=args.commit_sha,
        gates=gates,
        path_filter=path_filter,
        path_totals=path_totals,
        path_children=path_children,
        path_matched=path_matched,
    )

    if args.summary_file:
        with open(args.summary_file, "a", encoding="utf-8") as file:
            file.write(summary)
            file.write("\n")
    else:
        print(summary)

    print("JaCoCo coverage summary written.")

    if args.min_line_coverage is not None and "LINE" in totals:
        line_coverage = percentage(
            totals["LINE"]["covered"],
            totals["LINE"]["missed"],
        )

        if line_coverage is not None and line_coverage < args.min_line_coverage:
            print(
                f"Line coverage {line_coverage:.2f}% is below required minimum "
                f"of {args.min_line_coverage:.2f}%.",
                file=sys.stderr,
            )
            return 1

    # --- Lokales Quality Gate auswerten ---
    if failed:
        for metric, coverage, minimum in failed:
            actual = "keine Daten" if coverage is None else f"{coverage:.2f}%"
            print(
                f"::error::Lokales Quality Gate '{metric_label(metric)}': "
                f"{actual} < gefordertes Minimum {minimum:.2f}%"
            )
        return 1

    if gates:
        print(f"::notice::Lokales Quality Gate bestanden ({len(gates)} Grenze(n) geprueft).")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
