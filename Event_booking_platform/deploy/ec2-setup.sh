#!/usr/bin/env bash
# One-time server preparation for Ubuntu 22.04/24.04 or Amazon Linux 2023 (run on the EC2 instance):
#   sudo bash deploy/ec2-setup.sh
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then echo "Run as root (sudo)."; exit 1; fi

. /etc/os-release
LOGIN_USER="${SUDO_USER:-$( [ "$ID" = "ubuntu" ] && echo ubuntu || echo ec2-user )}"
ARCH="$(uname -m)"

echo ">> Installing Docker and Git ($PRETTY_NAME)"
case "$ID" in
  ubuntu|debian)
    apt-get update -y
    apt-get install -y ca-certificates curl git
    curl -fsSL https://get.docker.com | sh
    ;;
  amzn|rhel|centos|fedora)
    dnf install -y docker git
    mkdir -p /usr/local/lib/docker/cli-plugins
    curl -fsSL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-${ARCH}" \
      -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
    # "docker compose build" needs buildx 0.17+, which Amazon Linux does not ship.
    BX_ARCH="$(echo "$ARCH" | sed 's/x86_64/amd64/;s/aarch64/arm64/')"
    BX_VER="$(curl -fsSL https://api.github.com/repos/docker/buildx/releases/latest | grep '"tag_name"' | cut -d'"' -f4)"
    curl -fsSL "https://github.com/docker/buildx/releases/download/${BX_VER}/buildx-${BX_VER}.linux-${BX_ARCH}" \
      -o /usr/local/lib/docker/cli-plugins/docker-buildx
    chmod +x /usr/local/lib/docker/cli-plugins/docker-buildx
    ;;
  *)
    echo "Unsupported system: $ID. Install Docker with the Compose plugin manually."; exit 1 ;;
esac
systemctl enable --now docker
usermod -aG docker "$LOGIN_USER" || true

echo ">> Adding 2 GB swap (small instances run out of memory while building the Java image)"
if ! swapon --show | grep -q '/swapfile'; then
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  grep -q '/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

docker --version
docker compose version
echo ">> Done. Log out and back in so the docker group applies, then continue with the README, section 18 (Deployment on AWS EC2)."
