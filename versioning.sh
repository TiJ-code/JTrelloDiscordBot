#!/usr/bin/env bash

set -euo pipefail

POM="pom.xml"

fail() {
  echo "Error: $1" >&2
  exit 1
}

get_version() {
  mvn help:evaluate -Dexpression=project.version -q -DforceStdout
}

set_version() {
  local version="$1"

  mvn versions:set -DnewVersion="$version"

  rm -f "$POM.versionsBackup"
}

require_release_version() {
  local version="$1"

  if [[ "$version" == *-SNAPSHOT ]]; then
    fail "Current version is SNAPSHOT: $version"
  fi
}

parse_semver() {
  local version="$1"

  if ! [[ "$version" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
    fail "Expected semantic version (x.y.z), got: $version"
  fi

  MAJOR="${BASH_REMATCH[1]}"
  MINOR="${BASH_REMATCH[2]}"
  PATCH="${BASH_REMATCH[3]}"
}

release() {
  local version

  MAJOR=0
  MINOR=0
  PATCH=0

  version=$(get_version)

  [[ "$version" == *-SNAPSHOT ]] || fail "Version already released."

  local release_version
  release_version="${version%-SNAPSHOT}"

  set_version "$release_version"

  echo "Released v$release_version"
}

bump() {
  local version type next

  version=$(get_version)
  require_release_version "$version"

  parse_semver "$version"

  next="$(bump_version "$version" "$1")"

  set_version "$next"
  echo "$next"
}

bump_version() {
  local version="$1"
  local type="$2"

  [[ "$version" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]] \
    || fail "Invalid version"

  local major=${BASH_REMATCH[1]}
  local minor=${BASH_REMATCH[2]}
  local patch=${BASH_REMATCH[3]}

  case "$type" in
    major) ((major++)); minor=0; patch=0 ;;
    minor) ((minor++)); patch=0 ;;
    patch) ((patch++)) ;;
  esac

  echo "${major}.${minor}.${patch}-SNAPSHOT"
}

string() {
  local version
  version=$(get_version)

  require_release_version "$version"

  echo "v$version"
}

case "${1:-}" in
  --release)
    release
    ;;

  --bump=*)
    bump "${1#*=}"
    ;;

  --string)
    string
    ;;

  *)
    echo "Usage:"
    echo "./versioning.sh --release"
    echo "./versioning.sh --bump=(major|minor|patch)"
    echo "./versioning.sh --string"
    ;;
esac
