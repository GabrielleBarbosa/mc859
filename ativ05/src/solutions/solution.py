class Solution(list):
    def __init__(self, solution = None, *args, **kwargs):
        if solution != None :
            super().__init__(solution)
            self.cost = solution.cost
        else :
            super().__init__(*args, **kwargs)
            self.cost = float('inf')
        

    def __str__(self):
        return f"Solution: cost=[{self.cost}], size=[{len(self)}], elements={super().__str__()}"
