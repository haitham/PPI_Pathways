#set terminal postscript eps 22
#set output "hsa_practical_6.eps"
set style function linespoints
set xlabel "iteration"
set ylabel "success probability"
set xrange [1:400]
set yrange [0:1.01]
plot "_summary.txt" using 1 smooth unique title "scott", \
	'' using 2 smooth unique title "optimal-k", \
	'' using 3 smooth unique title "top 95%", \
	'' using 4 smooth unique title "bottom 95%", \
	'' using 5 smooth unique title "practical"