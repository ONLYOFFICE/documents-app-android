import os
import xml.etree.ElementTree as ET
from xml.dom import minidom
import shutil
import argparse

def get_strings(file_path):
    tree = ET.parse(file_path)
    root = tree.getroot()
    strings = {}
    for child in root:
        if 'translatable' in child.attrib and child.attrib['translatable'] == 'false':
            continue
        if child.tag == 'string':
            strings[child.attrib['name']] = child.text
        elif child.tag == 'plurals':
            plurals = {item.attrib['quantity']: item.text for item in child.findall('item')}
            strings[child.attrib['name']] = plurals
    return strings

def compare_strings(base_strings, compare_strings):
    missing_strings = {}
    for key, value in base_strings.items():
        if key not in compare_strings:
            missing_strings[key] = value
        elif isinstance(value, dict):
            if key in compare_strings and isinstance(compare_strings[key], dict):
                continue  # Skip if plurals already exist
            missing_plurals = {k: v for k, v in value.items() if k not in compare_strings[key]}
            if missing_plurals:
                missing_strings[key] = missing_plurals
    return missing_strings

def write_missing_strings_to_files(missing_strings_by_lang, output_dir, base_file_name):
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    for lang, missing_strings in missing_strings_by_lang.items():
        lang_dir = os.path.join(output_dir, lang)
        if not os.path.exists(lang_dir):
            os.makedirs(lang_dir)

        root = ET.Element("resources")
        for key, value in missing_strings.items():
            if isinstance(value, dict):
                plurals_element = ET.SubElement(root, "plurals", name=key)
                for quantity, text in value.items():
                    item_element = ET.SubElement(plurals_element, "item", quantity=quantity)
                    item_element.text = text
            else:
                string_element = ET.SubElement(root, "string", name=key)
                string_element.text = value

        tree = ET.ElementTree(root)
        output_file = os.path.join(lang_dir, base_file_name + '.xml')

        # Pretty print the XML
        xml_str = ET.tostring(root, encoding='utf-8')
        parsed_str = minidom.parseString(xml_str)
        pretty_xml_str = parsed_str.toprettyxml(indent="    ")

        with open(output_file, "w", encoding="utf-8") as f:
            f.write(pretty_xml_str)

def copy_base_file(base_file, output_dir, base_file_name):
    base_output_dir = os.path.join(output_dir, 'values')
    if not os.path.exists(base_output_dir):
        os.makedirs(base_output_dir)
    shutil.copy(base_file, os.path.join(base_output_dir, base_file_name))

def archive_output_dir(output_dir, archive_name):
    shutil.make_archive(archive_name, 'zip', output_dir)

def main():
    parser = argparse.ArgumentParser(description='Process XML files.')
    parser.add_argument('base_file_name', type=str, help='The base XML file name to compare against')
    args = parser.parse_args()

    base_name = args.base_file_name
    base_lang = 'values'
    base_file = f'src/main/res/{base_lang}/{base_name}.xml'

    base_strings = get_strings(base_file)

    languages = [d for d in os.listdir('src/main/res') if d.startswith('values-')]
    missing_strings_by_lang = {}

    for lang in languages:
        compare_file = f'src/main/res/{lang}/{base_name}.xml'
        if os.path.exists(compare_file):
            compare_strings_dict = get_strings(compare_file)
            missing_strings = compare_strings(base_strings, compare_strings_dict)
            if missing_strings:
                missing_strings_by_lang[lang] = missing_strings
        else:
            print(f'File not found for {lang}: {compare_file}')

    if missing_strings_by_lang:
        output_dir = 'output'
        write_missing_strings_to_files(missing_strings_by_lang, output_dir, base_name)
        copy_base_file(base_file, output_dir, base_name)
        archive_output_dir(output_dir, 'missing_strings')

if __name__ == '__main__':
    main()