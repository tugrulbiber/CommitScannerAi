import sys
import ollama
import re
import traceback

commit_message = sys.argv[1]
commit_diff = sys.argv[2]

prompt = f"""
Sen bir yapay zeka kod inceleyicisisin.

Aşağıda bir commit mesajı ve ona ait kod farkı (diff) verilecektir.

Senin görevin:
- Eğer commit’te herhangi bir sorun, kötü pratik ya da geliştirilebilecek bir yer varsa, sadece o kısmı yaz.
- Eğer bir problem yoksa yalnızca "Sorun tespit edilmedi." yaz.

Commit mesajı:
{commit_message}

Kod diff:
{commit_diff}
"""

def sanitize_text(text):
    # Windows ve Unix yol temizleme
    text = re.sub(r'[A-Z]:[\\/][^\s\n\r"\']+', '[local path]', text)
    text = re.sub(r'(\.\/|\.\.\/|\/)[^\s\n\r"\']+', '[local path]', text)
    return text

try:
    response = ollama.chat(model='llama3', messages=[
        {"role": "user", "content": prompt}
    ])
    content = response['message']['content']
    print(sanitize_text(content))
except Exception as e:
    tb = traceback.format_exc()
    clean_tb = sanitize_text(tb)
    print(f"⚠️ AI analiz başarısız:\n{clean_tb}")
