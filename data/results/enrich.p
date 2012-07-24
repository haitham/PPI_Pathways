set key bottom
set logscale xy
set xlabel "Rank"
set ylabel "Enrichment"
set size 0.8,0.8
set term postscript eps enhanced

set output "enrich-rank-rat.eps"
plot "rat" u 8 t "length = 6" with p pt 4, \
     "rat" u 6 t "length = 7" w p pt 2, \
     "rat" u 4 t "length = 8" w p pt 6, \
     "rat" u 2 t "length = 9" w p pt 1
!epstopdf enrich-rank-rat.eps



set output "enrich-rank-hsa.eps"
plot "hsa" u 6 t "length = 6" with p pt 4, \
     "hsa" u 4 t "length = 7" w p pt 2, \
     "hsa" u 2 t "length = 8" w p pt 6
!epstopdf enrich-rank-hsa.eps


unset logscale x
set xlabel "Path weight"

set output "enrich-score-rat.eps"
plot "rat" u 3:4 t "" w p
!epstopdf enrich-score-rat.eps


set output "enrich-score-hsa.eps"
plot "hsa" u 5:6 t "" w p
!epstopdf enrich-score-hsa.eps