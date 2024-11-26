def on_page_markdown(markdown: str, **kwargs) -> str:
    return markdown \
            .replace(r'=== "Kotlin"', r'=== ":material-language-kotlin: Kotlin"') \
            .replace(r'=== "Java"', r'=== ":material-language-java: Java"')
