# Bean — Python analytics

Generates a pink-themed PDF productivity report from the same MySQL DB
the Spring Boot app uses.

## Setup (one time)

```bash
cd analytics
pip install -r requirements.txt
```

## Use

The Spring Boot app calls this automatically when you click
**Download report** on the dashboard, but you can also run it by hand:

```bash
python report.py --user-id 1 --range weekly  --out report.pdf
python report.py --user-id 1 --range daily   --out report.pdf
python report.py --user-id 1 --range monthly --out report.pdf
```

## DB config

By default it reads the same credentials the app uses (`localhost:3306`,
`productivity_app`, `root`/your password). Override via env:

```bash
export BEAN_DB_HOST=localhost
export BEAN_DB_PORT=3306
export BEAN_DB_USER=root
export BEAN_DB_PASS=YOUR_PASS
export BEAN_DB_NAME=productivity_app
```

## How the Java side finds Python

`DashBoardController#downloadReport` runs `python` from `PATH` by default.
On systems where the binary is `python3`, set:

```bash
export BEAN_PYTHON=python3
```
