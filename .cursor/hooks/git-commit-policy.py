#!/usr/bin/env python3
"""Cursor hook: Chinese commits required; strip Co-authored-by Cursor (never keep it)."""
from __future__ import annotations

import json
import re
import sys

CO_LINE = re.compile(
    r"(?i)^\s*co-authored-by:\s*cursor\b.*$|^\s*co-authored-by:.*cursoragent@cursor\.com.*$"
)
CO_TRAILER_FLAG = re.compile(
    r"""(?ix)\s+--trailer(?:=|\s+)(['"])\s*Co-authored-by:\s*Cursor(?:\s*<[^'"]*>)?\s*\1"""
)
CO_TRAILER_FLAG2 = re.compile(
    r"""(?ix)\s+--trailer(?:=|\s+)(['"])\s*Co-authored-by:[^'"]*cursoragent@cursor\.com[^'"]*\1"""
)
GIT_COMMIT = re.compile(
    r"(?i)(^|[;&|\s])git(\s+(-C\s+\S+))*\s+commit(\s|$)"
)


def emit(obj: dict) -> None:
    print(json.dumps(obj, ensure_ascii=False))
    sys.exit(0)


def extract_command(data: dict) -> str:
    for key in ("command",):
        if isinstance(data.get(key), str):
            return data[key]
    for nest in ("tool_input", "input", "arguments", "params"):
        block = data.get(nest)
        if isinstance(block, dict) and isinstance(block.get("command"), str):
            return block["command"]
    return ""


def set_command(data: dict, command: str) -> dict:
    """Return updated_input payload Cursor understands for Shell."""
    if isinstance(data.get("tool_input"), dict):
        ti = dict(data["tool_input"])
        ti["command"] = command
        return {"tool_input": ti}
    if isinstance(data.get("input"), dict):
        ti = dict(data["input"])
        ti["command"] = command
        return {"input": ti}
    if isinstance(data.get("arguments"), dict):
        ti = dict(data["arguments"])
        ti["command"] = command
        return {"arguments": ti}
    return {"command": command}


def strip_coauthor_lines(text: str) -> str:
    kept = []
    for ln in text.splitlines():
        if CO_LINE.match(ln):
            continue
        kept.append(ln)
    return "\n".join(kept)


def strip_command(cmd: str) -> str:
    cmd = CO_TRAILER_FLAG.sub("", cmd)
    cmd = CO_TRAILER_FLAG2.sub("", cmd)

    def strip_m(m: re.Match[str]) -> str:
        q = m.group(1)
        body = strip_coauthor_lines(m.group(2)).strip("\n")
        return f"-m {q}{body}{q}"

    cmd = re.sub(r"""(?s)-m\s*(["'])(.*?)\1""", strip_m, cmd, count=1)

    def strip_heredoc(m: re.Match[str]) -> str:
        tag, body = m.group(1), m.group(2)
        body2 = strip_coauthor_lines(body)
        # preserve quote style roughly as <<'TAG'
        return f"<<'{tag}'\n{body2}\n{tag}"

    cmd = re.sub(r"<<-?\s*'(\w+)'\s*\n([\s\S]*?)\n\1\b", strip_heredoc, cmd)
    cmd = re.sub(r'<<-?\s*"(\w+)"\s*\n([\s\S]*?)\n\1\b', strip_heredoc, cmd)
    cmd = re.sub(r"<<-?\s*(\w+)\s*\n([\s\S]*?)\n\1\b", strip_heredoc, cmd)
    return cmd


def extract_message(cmd: str) -> str:
    chunks: list[str] = []
    for m in re.finditer(r"(?:-m|--message)(?:=|\s+)(\"(?:[^\"\\]|\\.)*\"|'(?:[^'\\]|\\.)*')", cmd):
        raw = m.group(1)
        chunks.append(raw[1:-1])
    for m in re.finditer(r"<<-?\s*['\"]?(\w+)['\"]?\s*\n([\s\S]*?)\n\1\b", cmd):
        chunks.append(m.group(2))
    return "\n".join(chunks).strip()


def main() -> None:
    raw = sys.stdin.read()
    try:
        data = json.loads(raw) if raw.strip() else {}
    except json.JSONDecodeError:
        emit({"permission": "allow"})

    cmd = extract_command(data)
    if not GIT_COMMIT.search(cmd):
        emit({"permission": "allow"})

    cleaned = strip_command(cmd)
    msg = extract_message(cleaned)

    if not msg:
        emit(
            {
                "permission": "deny",
                "user_message": "请使用中文提交说明（git commit -m 或 HEREDOC）。",
                "agent_message": "Use an explicit Chinese commit message via -m or HEREDOC. Never keep Co-authored-by: Cursor.",
            }
        )

    if not re.search(r"[\u4e00-\u9fff]", msg):
        emit(
            {
                "permission": "deny",
                "user_message": "提交说明必须使用中文（至少包含汉字）。",
                "agent_message": "Rewrite the commit message in Chinese (must include Han characters). Never keep Co-authored-by: Cursor.",
            }
        )

    result: dict = {"permission": "allow"}
    if cleaned != cmd:
        result["updated_input"] = set_command(data, cleaned)
        result["agent_message"] = "已从 git commit 命令中剥离 Co-authored-by: Cursor。"
    emit(result)


if __name__ == "__main__":
    main()
