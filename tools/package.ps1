[CmdletBinding()]
param(
    [Parameter()]
    [string]$ArchiveRoot = "C:\\Workspace\\Archives\\MobileInvoice",

    [Parameter()]
    [switch]$IncludeSnapshots,

    [Parameter()]
    [string[]]$SnapshotPaths
)

$ErrorActionPreference = 'Stop'

function Get-RepoRoot {
    $root = (git rev-parse --show-toplevel) 2>$null
    if (-not $root) {
        throw "Not inside a git repository. Run this script from within the repo." 
    }
    return $root.Trim()
}

function Ensure-Directory([string]$Path) {
    if (-not (Test-Path -LiteralPath $Path)) {
        New-Item -ItemType Directory -Path $Path | Out-Null
    }
}

function New-TimeStamp {
    return (Get-Date -Format 'yyyy-MM-dd_HHmmss')
}

function New-TempDir([string]$Name) {
    $tempRoot = [System.IO.Path]::GetTempPath()
    $tempDir = Join-Path $tempRoot $Name
    if (Test-Path -LiteralPath $tempDir) {
        Remove-Item -LiteralPath $tempDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $tempDir | Out-Null
    return $tempDir
}

function Copy-TrackedFilesToStaging([string]$RepoRoot, [string]$StagingDir) {
    Push-Location $RepoRoot
    try {
        $raw = git ls-files -z
        if (-not $raw) {
            throw "git ls-files returned no files. Is the repo empty or is git not available?"
        }

        $files = $raw -split "`0" | Where-Object { $_ -and $_.Trim() -ne '' }
        foreach ($relativePath in $files) {
            $source = Join-Path $RepoRoot $relativePath
            if (-not (Test-Path -LiteralPath $source)) {
                # If the index contains paths missing on disk, skip them.
                continue
            }
            $dest = Join-Path $StagingDir $relativePath
            $destDir = Split-Path -Parent $dest
            Ensure-Directory $destDir
            Copy-Item -LiteralPath $source -Destination $dest -Force
        }
    } finally {
        Pop-Location
    }
}

function Write-BundleInfo([string]$RepoRoot, [string]$StagingDir) {
    Push-Location $RepoRoot
    try {
        $branch = (git branch --show-current) 2>$null
        $commit = (git rev-parse --short HEAD) 2>$null
        $status = (git status --porcelain=v1) 2>$null

        $info = @()
        $info += "Bundle created: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
        if ($branch) { $info += "Branch: $($branch.Trim())" }
        if ($commit) { $info += "Commit: $($commit.Trim())" }
        $info += "RepoRoot: $RepoRoot"
        $info += ""
        $info += "This zip is intended to be a clean 'golden source' bundle."
        $info += "It is built from 'git ls-files' (tracked files), so build outputs/caches are excluded."
        $info += ""
        if ($status) {
            $info += "Working tree status at bundle time (porcelain):"
            $info += $status
        }

        $path = Join-Path $StagingDir "BUNDLE_INFO.txt"
        $info -join "`r`n" | Out-File -FilePath $path -Encoding UTF8
    } finally {
        Pop-Location
    }
}

function Compress-Staging([string]$StagingDir, [string]$ZipPath) {
    if (Test-Path -LiteralPath $ZipPath) {
        Remove-Item -LiteralPath $ZipPath -Force
    }
    Compress-Archive -Path (Join-Path $StagingDir '*') -DestinationPath $ZipPath -Force
}

function Zip-Folder([string]$SourceFolder, [string]$ZipPath) {
    if (-not (Test-Path -LiteralPath $SourceFolder)) {
        Write-Warning "Snapshot path not found: $SourceFolder"
        return
    }
    if (Test-Path -LiteralPath $ZipPath) {
        Remove-Item -LiteralPath $ZipPath -Force
    }
    Compress-Archive -Path (Join-Path $SourceFolder '*') -DestinationPath $ZipPath -Force
}

$repoRoot = Get-RepoRoot
Ensure-Directory $ArchiveRoot

$stamp = New-TimeStamp
$goldenName = "Mobile_Invoice_OCR_golden_source_$stamp"
$staging = New-TempDir -Name $goldenName

try {
    Copy-TrackedFilesToStaging -RepoRoot $repoRoot -StagingDir $staging
    Write-BundleInfo -RepoRoot $repoRoot -StagingDir $staging

    $goldenZip = Join-Path $ArchiveRoot ("$goldenName.zip")
    Compress-Staging -StagingDir $staging -ZipPath $goldenZip

    Write-Host "Golden source zip: $goldenZip"

    if ($IncludeSnapshots) {
        $workspaceRoot = Split-Path -Parent $repoRoot

        if (-not $SnapshotPaths -or $SnapshotPaths.Count -eq 0) {
            $SnapshotPaths = @(
                (Join-Path $workspaceRoot 'Mobile_Invoice_2.1'),
                (Join-Path $workspaceRoot 'Mobile_Invoice_Update_2.0')
            )
        }

        foreach ($snap in $SnapshotPaths) {
            $snapName = Split-Path -Leaf $snap
            $zipPath = Join-Path $ArchiveRoot ("$snapName`_$stamp.zip")
            Zip-Folder -SourceFolder $snap -ZipPath $zipPath
            if (Test-Path -LiteralPath $zipPath) {
                Write-Host "Snapshot zip: $zipPath"
            }
        }
    }
} finally {
    if (Test-Path -LiteralPath $staging) {
        Remove-Item -LiteralPath $staging -Recurse -Force
    }
}
