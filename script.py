import re

def process_kotlin_file(file_path):
    with open(file_path, 'r') as file:
        lines = file.readlines()
    
    processed_lines = []
    in_function = False
    brace_count = 0

    for i, line in enumerate(lines):
        stripped_line = line.strip()

        # Detect function definition
        if re.match(r'fun\s+\w+\(.*\)\s*{', stripped_line):
            in_function = True
            brace_count = stripped_line.count('{') - stripped_line.count('}')
        
        if in_function:
            brace_count += stripped_line.count('{')
            brace_count -= stripped_line.count('}')
        
            # If inside a function and found a catch block
            if 'catch' in stripped_line and '{' in stripped_line:
                lines.insert(i + 1, '        writeLogToSDCard(this, e.message.toString())
Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()\n')
                in_function = False
                brace_count = 0

            # End of function
            if brace_count == 0:
                in_function = False
        
        if in_function and not any(keyword in stripped_line for keyword in ['try', 'catch']):
            processed_lines.append('    try {\n')
            processed_lines.append(line)
            processed_lines.append('    } catch (e: Exception) {\n')
            processed_lines.append('        writeLogToSDCard(this, e.message.toString())
Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()\n')
            processed_lines.append('    }\n')
            in_function = False
        else:
            processed_lines.append(line)

    with open(file_path, 'w') as file:
        file.writelines(processed_lines)

if __name__ == "__main__":
    file_path = input("Enter the path of the Kotlin file: ").strip()
    process_kotlin_file(file_path)
    print(f"Processed file: {file_path}")
