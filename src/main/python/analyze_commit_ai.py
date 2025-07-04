import sys
import re
import ollama

if len(sys.argv) < 3:
    # Argüman yoksa test için sabit değerler kullan
    commit_message = "Test commit mesajı"
    commit_diff = """
    diff --git a/src/Main.java b/src/Main.java
    index e69de29..4b825dc 100644
    --- a/src/Main.java
    +++ b/src/Main.java
    @@ -0,0 +1,2 @@
    +public class Main {
    +    // test commit
    }
    """
else:
    commit_message = sys.argv[1]
    commit_diff = sys.argv[2]

# Local pathleri temizle (Windows + Unix)
commit_diff = re.sub(r'[A-Z]:\\\\[^\\s\n\r]+', '[local path]', commit_diff)
commit_diff = re.sub(r'(/[\w./\-]+)+', '[repo path]', commit_diff)

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
    feedback = response['message']['content']

    # AI çıktısındaki local pathleri temizle
    feedback = re.sub(r'[A-Z]:\\\\[^\\s\n\r]+', '[local path]', feedback)
    feedback = re.sub(r'(/[\w./\-]+)+', '[repo path]', feedback)

    print(feedback)

except Exception as e:
    print(f" AI analiz başarısız: {e}")
