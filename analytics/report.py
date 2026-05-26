#!/usr/bin/env python3
"""
Bean — Productivity report generator.

Reads from the same MySQL database the Spring Boot app uses, aggregates
tasks / pomodoro / notes per day-week-month, and writes a pink-themed
PDF report.

Invoked by Spring Boot via:
    python analytics/report.py --user-id 1 --range weekly --out /tmp/out.pdf
"""

import argparse
import os
import sys
from datetime import date, datetime, timedelta, timezone

try:
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    from matplotlib.backends.backend_pdf import PdfPages
    from matplotlib.patches import FancyBboxPatch
    import mysql.connector
except ImportError as e:
    print(f"Missing dependency: {e.name}. Run: pip install -r analytics/requirements.txt", file=sys.stderr)
    sys.exit(2)


# === Pink palette (matches the web UI) ===
PINK_50  = "#fff5fa"
PINK_100 = "#ffd6e0"
PINK_300 = "#ff8fb1"
PINK_400 = "#ff6b9d"
PINK_500 = "#e75a8a"
PINK_600 = "#c93f72"
TEXT     = "#4a2c3a"
TEXT_SOFT = "#8a6a78"


def db_config():
    """Read DB config from env, falling back to the app's defaults."""
    return {
        "host":     os.environ.get("BEAN_DB_HOST", "localhost"),
        "port":     int(os.environ.get("BEAN_DB_PORT", "3306")),
        "user":     os.environ.get("BEAN_DB_USER", "root"),
        "password": os.environ.get("BEAN_DB_PASS", "PARI@kem2006"),
        "database": os.environ.get("BEAN_DB_NAME", "productivity_app"),
    }


def connect():
    return mysql.connector.connect(**db_config())


def buckets_for(range_name: str):
    """Return [(label, start_utc, end_utc), ...] for the requested range."""
    today = date.today()
    if range_name == "daily":
        # last 7 days
        return [_day_bucket(today - timedelta(days=i)) for i in range(6, -1, -1)]
    if range_name == "weekly":
        # last 4 ISO weeks
        out = []
        for i in range(3, -1, -1):
            week_anchor = today - timedelta(weeks=i)
            monday = week_anchor - timedelta(days=week_anchor.weekday())
            sunday_next = monday + timedelta(days=7)
            label = f"W{monday.isocalendar().week}"
            out.append((label, _utc(monday), _utc(sunday_next)))
        return out
    if range_name == "monthly":
        # last 6 months
        out = []
        cur = date(today.year, today.month, 1)
        months = []
        for i in range(6):
            y, m = cur.year, cur.month - i
            while m <= 0:
                m += 12
                y -= 1
            months.append(date(y, m, 1))
        months.reverse()
        for i, first in enumerate(months):
            ny, nm = (first.year, first.month + 1) if first.month < 12 else (first.year + 1, 1)
            first_next = date(ny, nm, 1)
            out.append((first.strftime("%b"), _utc(first), _utc(first_next)))
        return out
    raise ValueError(f"unknown range: {range_name}")


def _day_bucket(d: date):
    return (d.strftime("%a"), _utc(d), _utc(d + timedelta(days=1)))


def _utc(d: date) -> datetime:
    return datetime(d.year, d.month, d.day, tzinfo=timezone.utc)


def fetch_metrics(conn, user_id: int, buckets):
    """Return dict with parallel arrays of label/tasks/focus_min/notes."""
    cur = conn.cursor()
    labels, tasks, focus_min, notes = [], [], [], []

    for label, start, end in buckets:
        labels.append(label)

        cur.execute(
            "SELECT COUNT(*) FROM tasks WHERE user_id=%s AND completed=1 "
            "AND updated_at >= %s AND updated_at < %s",
            (user_id, start, end),
        )
        tasks.append(int(cur.fetchone()[0] or 0))

        cur.execute(
            "SELECT COALESCE(SUM(duration_seconds), 0) FROM pomodoro_sessions "
            "WHERE user_id=%s AND is_completed=1 AND start_time >= %s AND start_time < %s",
            (user_id, start, end),
        )
        secs = int(cur.fetchone()[0] or 0)
        focus_min.append(secs // 60)

        cur.execute(
            "SELECT COUNT(*) FROM notes WHERE user_id=%s "
            "AND created_at >= %s AND created_at < %s",
            (user_id, start, end),
        )
        notes.append(int(cur.fetchone()[0] or 0))

    cur.close()
    return {"labels": labels, "tasks": tasks, "focus_min": focus_min, "notes": notes}


def fetch_user_name(conn, user_id: int) -> str:
    cur = conn.cursor()
    cur.execute("SELECT name FROM users WHERE id=%s", (user_id,))
    row = cur.fetchone()
    cur.close()
    return row[0] if row and row[0] else f"user #{user_id}"


def style_axes(ax):
    ax.spines["top"].set_visible(False)
    ax.spines["right"].set_visible(False)
    ax.spines["left"].set_color(PINK_100)
    ax.spines["bottom"].set_color(PINK_100)
    ax.tick_params(colors=TEXT_SOFT)
    ax.yaxis.label.set_color(TEXT_SOFT)
    ax.xaxis.label.set_color(TEXT_SOFT)
    ax.grid(True, axis="y", color=PINK_100, linewidth=0.7, alpha=0.6)
    ax.set_facecolor("white")


def render_cover(pdf, user_name: str, range_name: str, metrics):
    fig, ax = plt.subplots(figsize=(8.5, 11), facecolor=PINK_50)
    ax.set_axis_off()

    ax.text(0.5, 0.86, "Bean", ha="center", va="center", fontsize=48,
            color=PINK_500, family="serif", style="italic", fontweight="bold")
    ax.text(0.5, 0.81, "productivity report", ha="center", va="center",
            fontsize=12, color=PINK_600, fontweight="bold")

    ax.text(0.5, 0.74, f"For {user_name}", ha="center", fontsize=14, color=TEXT)
    ax.text(0.5, 0.71, f"Range: {range_name}    •    Generated {datetime.now():%Y-%m-%d %H:%M}",
            ha="center", fontsize=10, color=TEXT_SOFT)

    # Summary tiles
    totals = {
        "Tasks completed": sum(metrics["tasks"]),
        "Focus minutes":   sum(metrics["focus_min"]),
        "Notes created":   sum(metrics["notes"]),
    }
    xs = [0.18, 0.50, 0.82]
    for x, (label, value) in zip(xs, totals.items()):
        # rounded tile
        ax.add_patch(FancyBboxPatch((x - 0.13, 0.46), 0.26, 0.16,
                                    boxstyle="round,pad=0.02,rounding_size=0.025",
                                    linewidth=0, facecolor="white",
                                    transform=ax.transAxes))
        ax.text(x, 0.57, f"{value}", ha="center", va="center",
                fontsize=30, color=PINK_500, fontweight="bold",
                transform=ax.transAxes)
        ax.text(x, 0.50, label, ha="center", va="center",
                fontsize=9, color=TEXT_SOFT, fontweight="bold",
                transform=ax.transAxes)

    ax.text(0.5, 0.36, encouragement(totals), ha="center", fontsize=12,
            color=PINK_600, style="italic", wrap=True)

    pdf.savefig(fig)
    plt.close(fig)


def encouragement(totals):
    t = totals["Tasks completed"]
    f = totals["Focus minutes"]
    if t == 0 and f == 0:
        return "A fresh page is a beautiful thing.\nLet's start a tiny session today 💖"
    if f >= 120:
        return f"{f} minutes of focus — that's incredible. Keep it cozy."
    if t >= 5:
        return f"You closed {t} tasks. That's a real win. ✨"
    return "Small steps add up. Be kind to yourself."


def render_charts(pdf, metrics, range_name):
    labels = metrics["labels"]

    fig, axes = plt.subplots(3, 1, figsize=(8.5, 11), facecolor=PINK_50)
    fig.subplots_adjust(hspace=0.55, top=0.93, bottom=0.07, left=0.12, right=0.94)
    fig.suptitle(f"{range_name.capitalize()} breakdown",
                 fontsize=16, color=PINK_600, fontweight="bold", y=0.97)

    # Tasks completed (bar)
    ax = axes[0]
    ax.bar(labels, metrics["tasks"], color=PINK_400, edgecolor="white", linewidth=2)
    ax.set_title("Tasks completed", color=PINK_500, fontweight="bold", loc="left")
    style_axes(ax)

    # Focus minutes (line w/ fill)
    ax = axes[1]
    ax.plot(labels, metrics["focus_min"], color=PINK_500, marker="o",
            markerfacecolor=PINK_500, markersize=7, linewidth=2.5)
    ax.fill_between(range(len(labels)), metrics["focus_min"], color=PINK_300, alpha=0.35)
    ax.set_title("Focus minutes", color=PINK_500, fontweight="bold", loc="left")
    style_axes(ax)

    # Notes (bar, paler)
    ax = axes[2]
    ax.bar(labels, metrics["notes"], color=PINK_300, edgecolor="white", linewidth=2)
    ax.set_title("Notes created", color=PINK_500, fontweight="bold", loc="left")
    style_axes(ax)

    pdf.savefig(fig)
    plt.close(fig)


def render_data_table(pdf, metrics, range_name):
    fig, ax = plt.subplots(figsize=(8.5, 11), facecolor=PINK_50)
    ax.set_axis_off()
    ax.text(0.5, 0.95, f"{range_name.capitalize()} data", ha="center",
            fontsize=16, color=PINK_600, fontweight="bold")

    rows = list(zip(metrics["labels"], metrics["tasks"],
                    metrics["focus_min"], metrics["notes"]))
    table = ax.table(
        cellText=rows,
        colLabels=["Period", "Tasks", "Focus (min)", "Notes"],
        cellLoc="center",
        loc="center",
        colColours=[PINK_300, PINK_300, PINK_300, PINK_300],
    )
    table.auto_set_font_size(False)
    table.set_fontsize(11)
    table.scale(1, 1.8)

    for (r, c), cell in table.get_celld().items():
        cell.set_edgecolor("white")
        if r == 0:
            cell.set_text_props(color="white", fontweight="bold")
        else:
            cell.set_facecolor("white" if r % 2 == 0 else PINK_50)
            cell.set_text_props(color=TEXT)

    pdf.savefig(fig)
    plt.close(fig)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--user-id", type=int, required=True)
    ap.add_argument("--range", choices=["daily", "weekly", "monthly"], default="weekly")
    ap.add_argument("--out", required=True, help="Output PDF path")
    args = ap.parse_args()

    conn = connect()
    try:
        buckets = buckets_for(args.range)
        metrics = fetch_metrics(conn, args.user_id, buckets)
        user_name = fetch_user_name(conn, args.user_id)
    finally:
        conn.close()

    os.makedirs(os.path.dirname(os.path.abspath(args.out)), exist_ok=True)
    with PdfPages(args.out) as pdf:
        render_cover(pdf, user_name, args.range, metrics)
        render_charts(pdf, metrics, args.range)
        render_data_table(pdf, metrics, args.range)

    print(f"OK: wrote {args.out}")


if __name__ == "__main__":
    main()
