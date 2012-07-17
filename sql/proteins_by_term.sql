select distinct s.genus, s.species, gp.symbol, db.xref_dbname, db.xref_key, term.acc from species as s
inner join gene_product as gp on gp.species_id = s.id
inner join dbxref as db on gp.dbxref_id = db.id
INNER JOIN association ON gp.id = association.gene_product_id
INNER JOIN graph_path ON graph_path.term2_id=association.term_id
inner join term on term.id = graph_path.term1_id
where s.ncbi_taxa_id = '6239'
and (term.acc = 'GO:0005886'  or term.acc = 'GO:0004872');
#and (term.acc = 'GO:0000988' or term.acc = ' GO:0001071' or term.acc = ' GO:0006351');


select distinct db.xref_key from species as s
inner join gene_product as gp on gp.species_id = s.id
inner join dbxref as db on gp.dbxref_id = db.id
INNER JOIN association ON gp.id = association.gene_product_id
INNER JOIN graph_path ON graph_path.term2_id=association.term_id
inner join term on term.id = graph_path.term1_id
where s.ncbi_taxa_id = '7227'
and db.xref_dbname = 'UniProtKB'
and (term.acc = 'GO:0005886'  or term.acc = 'GO:0004872');
#and (term.acc = 'GO:0000988' or term.acc = ' GO:0001071' or term.acc = ' GO:0006351');