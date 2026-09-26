#!/usr/bin/env bash
# One-time server preparation for Ubuntu 22.04/24.04 (run on the EC2 instance):
#   curl -fsSL <raw url of this file> | sudo bash        or:   sudo bash deploy/ec2-setup.sh
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then echo "Run as root (sudo)."; exit 1; fi

echo ">> Installing Docker"
apt-get update -y
apt-get install -y ca-certificates curl git
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker
usermod -aG docker "${SUDO_USER:-ubuntu}" || true

echo ">> Adding 2 GB swap (small instances run out of memory while building the Java image)"
if ! swapon --show | grep -q '/swapfile'; then
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

echo ">> Done. Log out and back in so the docker group applies, then follow deploy/aws-ec2.md."
