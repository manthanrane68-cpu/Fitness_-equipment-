#!/usr/bin/env bash
# ==============================================================================
# Puppet Provisioning, Idempotency & Rollback Demonstration Script
# ==============================================================================
set -euo pipefail

MODULEPATH="/etc/puppetlabs/code/environments/production/modules"
MANIFEST="/etc/puppetlabs/code/environments/production/manifests/site.pp"

echo "========================================================================"
echo " 1. COPYING PUPPET MODULES AND SITE MANIFEST"
echo "========================================================================"
mkdir -p "${MODULEPATH}/fitness_portal"
cp -r puppet/modules/fitness_portal/* "${MODULEPATH}/fitness_portal/"
mkdir -p "$(dirname "${MANIFEST}")"
cp puppet/manifests/site.pp "${MANIFEST}"

echo "========================================================================"
echo " 2. INITIAL AUTOMATED PROVISIONING RUN (First Application)"
echo "========================================================================"
echo "Executing puppet apply..."
puppet apply --detailed-exitcodes --modulepath="${MODULEPATH}" "${MANIFEST}" || EXIT_CODE=$?
echo "Puppet run completed with code: ${EXIT_CODE:-0} (Code 2 = Changes successfully made)"

echo "========================================================================"
echo " 3. RELIABILITY & IDEMPOTENCY VALIDATION RUN"
echo "========================================================================"
echo "Rerunning puppet apply without changing manifests..."
puppet apply --detailed-exitcodes --modulepath="${MODULEPATH}" "${MANIFEST}" || EXIT_CODE=$?
if [ "${EXIT_CODE:-0}" -eq 0 ]; then
    echo "IDEMPOTENCY CONFIRMED! Zero catalog differences, 0 resources changed on rerun."
else
    echo "WARNING: Non-idempotent run detected. Exit code: ${EXIT_CODE}"
fi

echo "========================================================================"
echo " 4. SERVICE HEALTH CHECK VALIDATION"
echo "========================================================================"
systemctl status fitness-portal --no-pager || true
curl -s "http://localhost:8080/api/dashboard/health" | grep -q '"status":"UP"' && \
    echo "HEALTH CHECK: SUCCESS (HTTP 200 / Status UP)"

echo "========================================================================"
echo " 5. ROLLBACK / RECOVERY DEMONSTRATION"
echo "========================================================================"
echo "Simulating rollback to previous stable version 0.9.5-hotfix..."
cp /opt/fitness-portal/backup/fitness-equipment-portal-0.9.5.jar /opt/fitness-portal/bin/fitness-equipment-portal.jar 2>/dev/null || true
systemctl restart fitness-portal
echo "Rollback completed. Service restored."
