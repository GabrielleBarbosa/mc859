from src.problems.sc_qbf.sc_qbf import SC_QBF

class SC_QBF_Inverse(SC_QBF):
    def evaluate_qbf(self):
        return -super().evaluate_qbf()

    def evaluate_insertion_qbf(self, i: int):
        return -super().evaluate_insertion_qbf(i)

    def evaluate_removal_qbf(self, i: int):
        return -super().evaluate_removal_qbf(i)

    def evaluate_exchange_qbf(self, in_elem: int, out_elem: int):
        return -super().evaluate_exchange_qbf(in_elem, out_elem)
