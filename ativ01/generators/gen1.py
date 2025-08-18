import random

def write_file(n, S, A):
    with open(f"data/gen1_{n}.txt", 'w') as file:
        file.write(str(n))
        file.write('\n')
        file.write(' '.join([str(len(s)) for s in S]))
        file.write('\n')
        for s in S:
            file.write(' '.join([str(si) for si in s]))
            file.write('\n')
        for i in range(n):
            file.write(' '.join([str(a) for a in A[i][i:]]))
            file.write('\n')



def main():
    sizes = [25, 50, 100, 200, 400]
    for n in sizes:
        S = [[] for _ in range(n)]
        A = [[] for _ in range(n)]
        numbers = [k for _ in range(4) for k in range(1,n+1) ] # fixed size 4 for subsets S
        for i in range(n):
            S[i] = numbers[i*4:(i*4)+4]

            A[i] = [0 for _ in range(n)]
            for j in range(i, n):
                A[i][j] = random.randint(-10, 10) 

        write_file(n, S, A)
        
if __name__=='__main__':
    main()