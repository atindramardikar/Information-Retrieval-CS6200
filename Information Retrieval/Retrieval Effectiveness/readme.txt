Goal:
To evaluate retrieval effectiveness by calculating
1- Precision
2- Recall
3- P@K, where K = 20
4- Normalized Discounted Cumulative Gain (NDCG)
5- Mean Average Precision (MAP)
 

The relevance judgements for the CACM test collections are available here http://www.search-engines-book.com/collections/
 

The queries of interest are a subset of those in HW3 and HW4 (see below). Therefore, you can simply use the results obtained in either homework. CACM query IDs are provided below between parentheses (ID)
 

portable operating systems (12)
code optimization for space efficiency (13)
parallel algorithms (19)
 

The relevance judgements are interpreted as follows:
 

Q_ID    Q0   DOC_ID   d
 

Q_ID: Query ID (in this case, you have 12, 13, and 19)
Q0: To be ignored
DOC_ID: A unique document ID, basically the name of the HTML file
d: A digit representing relevance level, where 0 means non-relevant and 1 means relevant