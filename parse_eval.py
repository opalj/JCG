categories_js = {
    'AF': 'Anonymous Functions',
    'B': 'Built-ins',
    'C': 'Classes',
    'CL': 'Closures',
    'CO': 'Containers',
    'DCG': 'Dynamic Code Generation',
    'DPA': 'Dynamic Property Access',
    'HOF': 'Higher Order Functions',
    'M': 'Modules',
    'ND': 'Nested Definitions',
    'P': 'Prototypes',
    'R': 'Recursion',
}

categories_py = {
    'AF': 'Anonymous Functions',
    'B': 'Built-ins',
    'C': 'Classes',
    'CL': 'Closures',
    'CO': 'Containers',
    'D': 'Decorators',
    'DCG': 'Dynamic Code Generation',
    'DPA': 'Dynamic Property Access',
    'HOF': 'Higher Order Functions',
    'M': 'Modules',
    'ND': 'Nested Definitions',
    'OO': 'Operator Overloading',
    'R': 'Recursion',
    'SM': 'Static Methods',
}

# Read js results file (evaluation-result.tsv)
# Structure:
# js-callgraph: {Builtins: [S, S, S, U, E]}

def getResults(file_path, categories): 
    frameworks = {}
    with open(file_path) as f:
        # first line is header
        headers = f.readline().split("\t")
        
        for line in f:
            line = line.strip().split("\t")
            framework = line[0]
            
            if framework not in frameworks:
                    frameworks[framework] = {}

            # iterate through results for framework
            for i in range(1, len(line)):
                # remove number at end, this only works because we have less than 9 test per category, otherwise use regex
                category_key = headers[i].strip()[:-1]
                category = categories[category_key]
                
                if category not in frameworks[framework]:
                    print("Adding category", category)
                    frameworks[framework][category] = []

                frameworks[framework][category].append(line[i])

    return frameworks

def printResults(frameworks):
    for framework, categories in frameworks.items():
        print(framework)
        for category, results in frameworks[framework].items():
            print(f"\t{category}")
            print(f"\t\t{results.count('S')}/{len(results)}")   
        print("\n")

def printLatexTable(frameworks):
    keys = list(frameworks.keys())
    header = "Feature\t\t\t\t & " + " & ".join(keys) + " \\\\" + " \\midrule"
    print(header)
    num_of_categories = len(frameworks[keys[0]])
    categories = list(frameworks[keys[0]].keys())
    for i in range(num_of_categories):
        row = f"{categories[i]} \t\t\t\t & "
        for framework in frameworks:
            results = frameworks[framework][categories[i]]
            row += f"{results.count('S')}/{len(results)} &\t\t\t"
        print(row + " \\\\")

    # Add final line with totals
    row = "\\textbf{Total}\t\t\t\t & "
    total_tests = 0
    total_success = 0
    for framework in frameworks:
        for category in frameworks[framework]:
            results = frameworks[framework][category]
            total_tests += len(results)
            total_success += results.count('S')
        
        row += f"{total_success}/{total_tests} &\t\t\t"
        total_tests = 0
        total_success = 0
    print(row + " \\\\")

        


print("JAVASCRIPT\n")
printLatexTable(getResults("docker-results/js/evaluation-result.tsv", categories_js))
print("PYTHON\n")
printLatexTable(getResults("docker-results/python/evaluation-result.tsv", categories_py))
