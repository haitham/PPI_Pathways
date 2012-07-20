set xlabel "confidence"
set ylabel "time(ms)"
set xrange [0:1.01]
set yrange [1:1100000]
set key top left
plot "8.haitham.out" using 3:5 smooth unique title "optimal-k", \
	"8.sharan.out" using 2:3 smooth unique title "scott"