$ErrorActionPreference = "Stop"

$documentsDirectory = Join-Path $PSScriptRoot "documents"
New-Item -ItemType Directory -Path $documentsDirectory -Force | Out-Null

$documents = @(
    @{
        Url = "https://www.gutenberg.org/ebooks/11.txt.utf-8"
        FileName = "alice-wonderland.txt"
    },
    @{
        Url = "https://www.gutenberg.org/files/11/11-h/11-h.htm"
        FileName = "alice-wonderland.html"
    },
    @{
        Url = "https://www.gutenberg.org/ebooks/11.epub.noimages"
        FileName = "alice-wonderland.epub"
    }
)

foreach ($document in $documents) {
    $target = Join-Path $documentsDirectory $document.FileName
    Write-Host "Preuzimam $($document.FileName)..."
    Invoke-WebRequest -Uri $document.Url -OutFile $target
}

Get-ChildItem -Path $documentsDirectory | Select-Object Name, Length, FullName
