SELECT db.xref_key, t.acc FROM gene_product as gp
inner join association as a on gp.id = a.gene_product_id
inner join term as t on t.id = a.term_id
inner join dbxref as db on db.id = gp.dbxref_id
inner join species as s on s.id = gp.species_id
where s.ncbi_taxa_id = 562 and db.xref_dbname = 'UniProtKB';