INSERT INTO categories (id,name)
VALUES
  (1,'Sedans'),
  (2,'SUVs & Crossovers'),
  (3,'Trucks & Pickups'),
  (4,'Coupes & Convertibles'),
  (5,'Hatchbacks'),
  (6,'Electric & Hybrid Cars'),
  (7,'Luxury Vehicles'),
  (8,'Sports Cars'),
  (9,'Commercial Vehicles'),
  (10,'Off-Road & 4x4')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;
