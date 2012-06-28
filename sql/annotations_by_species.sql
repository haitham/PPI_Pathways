SELECT distinct dbxref.xref_key, term.acc FROM term
 INNER JOIN graph_path ON (term.id=graph_path.term1_id)
 INNER JOIN association ON (graph_path.term2_id=association.term_id)
 INNER JOIN gene_product ON (association.gene_product_id=gene_product.id)
 INNER JOIN species ON (gene_product.species_id=species.id)
 INNER JOIN dbxref ON (gene_product.dbxref_id=dbxref.id)
where species.ncbi_taxa_id = 83334
and dbxref.xref_dbname = 'UniProtKB';