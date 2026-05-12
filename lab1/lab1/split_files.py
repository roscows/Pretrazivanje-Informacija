#!/usr/bin/env python3
"""
Pomoćna Python skripta za deljenje fajlova.
Ekvivalent Java klase FileSplitter.
Koristite ako ne možete da pokrenete Java aplikaciju za deljenje.
"""
import os, math

INPUT_DIR  = "data/original"
OUTPUT_DIR = "data/split"
PARTS      = 100

os.makedirs(OUTPUT_DIR, exist_ok=True)

files = [f for f in os.listdir(INPUT_DIR) if f.endswith(".txt")]
print(f"Pronađeno {len(files)} fajlova, deljenje na {PARTS} delova svaki...")

total = 0
for fname in files:
    path = os.path.join(INPUT_DIR, fname)
    with open(path, "rb") as f:
        content = f.read()
    
    size = len(content)
    part_size = math.ceil(size / PARTS)
    base = fname.replace(".txt", "")
    
    for i in range(PARTS):
        start = i * part_size
        end   = min(start + part_size, size)
        if start >= size:
            break
        part_name = f"{base}_part_{i+1:03d}.txt"
        with open(os.path.join(OUTPUT_DIR, part_name), "wb") as out:
            out.write(content[start:end])
    
    print(f"  {fname} -> {PARTS} delova")
    total += PARTS

print(f"Ukupno kreirano: {total} fajlova u '{OUTPUT_DIR}'")
