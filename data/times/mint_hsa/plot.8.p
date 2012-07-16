set xlabel "confidence"
set ylabel "time(ms)"
set xrange [0:0.9]
set yrange [1:200000000]
set key top left
plot "8.haitham.out" using 2:3 smooth unique title "optimal-k", \
	"8.sharan.out" using 2:3 smooth unique title "scott"