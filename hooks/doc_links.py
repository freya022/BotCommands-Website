import logging
import re

import requests
from mkdocs import plugins


class IdentifierNotFoundException(Exception):
    def __init__(self, message: str):
        super().__init__(message)


log = logging.getLogger(f"mkdocs.plugins.{__name__}")

errors: list = []
session = requests.Session()


def get_link_representation(identifier: str) -> dict | None:
    r = session.get('http://localhost:16069/link', params={'identifier': identifier},
                    headers={'Accept': 'application/json'})
    if r.status_code != requests.codes.ok:
        log.debug(f"Received status code {r.status_code} for identifier {identifier}")
        return None
    return r.json()


def replace_identifier_with_recommended(match: re.Match[str]) -> str | None:
    identifier = match.group(1)
    link_representation = get_link_representation(identifier)
    if link_representation is None:
        errors.append(identifier)
        return None

    return f'[`{link_representation["label"]}`]({link_representation["url"]})'


def replace_identifier_keep_label(match: re.Match[str]) -> str | None:
    label = match.group(1)
    identifier = match.group(2)
    link_representation = get_link_representation(identifier)
    if link_representation is None:
        errors.append(identifier)
        return None

    return f'[{label}]({link_representation["url"]})'


def on_pre_build(**kwargs):
    global errors
    errors = []


@plugins.event_priority(100)
def on_page_markdown(markdown: str, **kwargs) -> str:
    markdown = re.sub(r'\[(.*?)]\[\[([\w#(), .]+)]]', replace_identifier_keep_label, markdown)
    markdown = re.sub(r'\[\[([\w#(), .]+)]]', replace_identifier_with_recommended, markdown)
    return markdown


def on_post_build(**kwargs):
    if len(errors) > 0:
        raise IdentifierNotFoundException(f"Some identifiers were not found:\n{', '.join(errors)}")


def on_shutdown():
    session.close()


if __name__ == '__main__':
    on_pre_build()
    print(on_page_markdown(r'[[BService]]'))
    on_post_build()
    on_shutdown()
