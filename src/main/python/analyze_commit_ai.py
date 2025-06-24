import sys
import ollama

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

try:
    response = ollama.chat(model='llama3', messages=[
        {"role": "user", "content": prompt}
    ])
    print(response['message']['content'])
except Exception as e:
    print(f"⚠️ AI analiz başarısız: {e}")
