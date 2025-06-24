import subprocess

def generate_ai_feedback(commit_message: str, commit_diff: str) -> str:
    prompt = f"""
Aşağıda bir commit mesajı ve kod farkı (diff) verilmiştir.

Commit mesajı:
{commit_message}

Kod farkı:
{commit_diff}

Yapay zeka olarak bu commit hakkında yorum yap:
1. Ne amaçlanmış?
2. Riskli bir değişiklik var mı?
3. Kod daha iyi nasıl yazılabilir?
4. Geliştiriciye geri bildirim ver (Türkçe olarak).
"""

    result = subprocess.run(
        ["ollama", "run", "llama2", prompt],
        capture_output=True,
        text=True
    )

    return result.stdout.strip()
