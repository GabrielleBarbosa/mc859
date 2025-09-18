import math

class Solution(list):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.cost = float('inf')

    def __str__(self):
        return f"Solution: cost=[{self.cost}], size=[{len(self)}], elements={super().__str__()}"
