create table if not exists embeddings (
    id uuid primary key default gen_random_uuid(),
    category varchar not null,
    subcategory varchar,
    content text,
    embedding vector(768)
);

create index if not exists idx_embeddings_category on embeddings using btree (category);
create index if not exists idx_embeddings_subcategory on embeddings using btree (subcategory);

create table if not exists centroids (
    id uuid primary key default gen_random_uuid(),
    category varchar not null,
    subcategory varchar,
    embedding vector(768)
);

create unique index if not exists idx_centroids_unique on centroids using btree (category, subcategory) nulls not distinct;
create index if not exists idx_centroids_category on centroids using btree (category);
create index if not exists idx_centroids_subcategory on centroids using btree (subcategory);

create or replace function compute_centroid (_category varchar, _subcategory varchar default null)
    returns void
    as $$
    declare
        new_centroid centroids.embedding%type;
    begin
        select avg(embedding) into new_centroid
            from embeddings
            where category = _category
              and subcategory is not distinct from _subcategory;

        insert into centroids(category, subcategory, embedding)
            values (_category, _subcategory, new_centroid)
            on conflict (category, subcategory) do
                update set embedding = excluded.embedding;
    end;
    $$ language plpgsql;

create or replace function incorporate_new_embedding()
    returns trigger
    as $$
    begin
        perform compute_centroid(NEW.category, NEW.subcategory);
        return NEW;
    end;
    $$ language plpgsql;

create or replace function process_removed_embedding()
    returns trigger
    as $$
    declare
        num_embeddings int;
    begin
        select count(*) into num_embeddings
            from embeddings
            where category = OLD.category
            and subcategory is not distinct from OLD.subcategory;

        if num_embeddings = 0 then
            delete from centroids
                where category = OLD.category
                and subcategory is not distinct from OLD.subcategory;

            return OLD;
        end if;

        perform compute_centroid(OLD.category, OLD.subcategory);
        return OLD;
    end;
    $$ language plpgsql;

create or replace trigger trigger_update_centroids_new_data after insert or update on embeddings
    for each row
    execute function incorporate_new_embedding();

create or replace trigger trigger_update_centroids_removed_data after delete on embeddings
    for each row
    execute function process_removed_embedding();
