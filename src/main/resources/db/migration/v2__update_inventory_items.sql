-- Krājumu tabulas atjaunināšana
-- Maina quantity lauka tipu no INTEGER uz DECIMAL precīzākiem aprēķiniem

-- Maina quantity lauka tipu uz DECIMAL(10,2) precīzākiem daudzuma aprēķiniem
-- Tas ļauj glabāt decimālskaitļus (piemēram, 15.75 kg, 2.5 litri)
ALTER TABLE inventory_items
ALTER COLUMN quantity TYPE DECIMAL(10,2);

-- Pievieno komentāru par izmaiņu
COMMENT ON COLUMN inventory_items.quantity IS 'Produkta daudzums noliktavā (ar decimālzīmēm precīziem aprēķiniem)';

-- Pārbauda, ka visi esošie ieraksti atbilst jaunajām prasībām
-- (šis vaicājums neatgriež datus, bet pārbauda datu integritāti)
DO $$
BEGIN
    -- Pārbauda, vai ir ieraksti ar negatīvu daudzumu
    IF EXISTS (SELECT 1 FROM inventory_items WHERE quantity < 0) THEN
        RAISE EXCEPTION 'Atrasti ieraksti ar negatīvu krājumu daudzumu. Lūdzu, labojiet datus pirms turpināt.';
END IF;

    -- Informē par veiksmīgu atjaunināšanu
    RAISE NOTICE 'Krājumu tabulas quantity lauks veiksmīgi atjaunināts uz DECIMAL(10,2)';
END $$;