#set terminal postscript eps 22
#set output "hsa_practical_6.eps"
#set nologscale y

set size 0.6,0.6
set style function linespoints
set xlabel "iteration"
set ylabel "confidence"
set key bottom
set terminal postscript eps  enhanced

set yrange [0:1.01]

set xrange [1:100]
set output "hsa4.eps"
plot "mint_hsa_4/_summary.txt" \
	 using 5 smooth unique title "practical",\
	'' using 2 smooth unique title "optimal-k", \
        '' using 1 smooth unique title "scott" 
!epstopdf hsa4.eps


set xrange [1:400]
set output "hsa6.eps"
plot "mint_hsa_6/_summary.txt" \
	 using 5 smooth unique title "practical",\
	'' using 2 smooth unique title "optimal-k", \
        '' using 1 smooth unique title "scott" 
!epstopdf hsa6.eps


set xrange [1:500]
set output "hsa8.eps"
plot "mint_hsa_8/_summary.txt" \
	 using 5 smooth unique title "practical",\
	'' using 2 smooth unique title "optimal-k", \
        '' using 1 smooth unique title "scott" 
!epstopdf hsa8.eps


set xrange [1:100]
set output "rat4.eps"
plot "mint_rat_4/_summary.txt" \
	 using 5 smooth unique title "practical",\
	'' using 2 smooth unique title "optimal-k", \
        '' using 1 smooth unique title "scott" 
!epstopdf rat4.eps


set xrange [1:400]
set output "rat6.eps"
plot "mint_rat_6/_summary.txt" \
	 using 5 smooth unique title "practical",\
	'' using 2 smooth unique title "optimal-k", \
        '' using 1 smooth unique title "scott" 
!epstopdf rat6.eps


set xrange [1:500]
set output "rat8.eps"
plot "mint_rat_8/_summary.txt" \
	 using 5 smooth unique title "practical",\
	'' using 2 smooth unique title "optimal-k", \
        '' using 1 smooth unique title "scott" 
!epstopdf rat8.eps

