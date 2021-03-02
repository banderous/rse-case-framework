insert into cases select id, 2542345663454321  from generate_series(2542345663454323, 2542345663474324) id;
insert into events select id, 'CreateClaim', id, now(), 'Created', 'super@gmail.com'
from generate_series(2542345663454323, 2542345663474324) id;

insert into parties select id, id, '{}'::jsonb
from generate_series(2542345663454323, 2542345663474324) id;

analyze;
