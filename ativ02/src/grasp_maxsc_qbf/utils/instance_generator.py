"""
Instance generator for testing
"""


def create_sample_instance():
    """Create a sample QBF-SC instance for testing in the new format"""
    content = """4
2 3 2 1
1 2
2 3 4
1 4
3
10 -2 3 1
5 0 -1
8 4
-2"""
    
    with open('sample_qbf_sc_new.txt', 'w') as f:
        f.write(content)
    
    return 'sample_qbf_sc_new.txt'