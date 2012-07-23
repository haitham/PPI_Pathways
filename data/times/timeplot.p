set xlabel "confidence"
set ylabel "time [sec]"
set xrange [0:1.01]
#set yrange [1:260000]
set key top left
#set logscale y
set size 0.6,0.6

set terminal postscript eps  enhanced

set yrange [1:260]
set output "time_rat6.eps"
plot "mint_rat/6.haitham.out" using 3:5 smooth unique title "optimal-k", \
	"mint_rat/6.sharan.out" using 2:3 smooth unique title "scott"
!epstopdf time_rat6.eps

set yrange [1:500]
set output "time_rat7.eps"
plot "mint_rat/7.haitham.out" using 3:5 smooth unique title "optimal-k", \
	"mint_rat/7.sharan.out" using 2:3 smooth unique title "scott"
!epstopdf time_rat7.eps

set yrange [1:1050]
set output "time_rat8.eps"
plot "mint_rat/8.haitham.out" using 3:5 smooth unique title "optimal-k", \
	"mint_rat/8.sharan.out" using 2:3 smooth unique title "scott"
!epstopdf time_rat8.eps

set yrange [1:2210]
set output "time_rat9.eps"
plot "mint_rat/9.haitham.out" using 3:5 smooth unique title "optimal-k", \
	"mint_rat/9.sharan.out" using 2:3 smooth unique title "scott"
!epstopdf time_rat9.eps

set yrange [1:32000]
set output "time_hsa6.eps"
plot "mint_hsa/6.haitham.out" using 2:3 smooth unique title "optimal-k", \
	"mint_hsa/6.sharan.out" using 2:3 smooth unique title "scott"
!epstopdf time_hsa6.eps

set yrange [1:86900]
set output "time_hsa7.eps"
plot "mint_hsa/7.haitham.out" using 2:3 smooth unique title "optimal-k", \
	"mint_hsa/7.sharan.out" using 2:3 smooth unique title "scott"
!epstopdf time_hsa7.eps

set yrange [1:200000]
set output "time_hsa8.eps"
plot "mint_hsa/8.haitham.out" using 2:3 smooth unique title "optimal-k", \
	"mint_hsa/8.sharan.out" using 2:3 smooth unique title "scott"
!epstopdf time_hsa8.eps

