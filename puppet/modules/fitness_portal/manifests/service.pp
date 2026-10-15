# ==============================================================================
# Puppet Class: fitness_portal::service
# Manages systemd daemon and Nginx service states
# ==============================================================================

class fitness_portal::service {
  # Fitness Portal systemd service
  service { 'fitness-portal':
    ensure     => running,
    enable     => true,
    hasstatus  => true,
    hasrestart => true,
  }

  # Nginx reverse proxy service
  if $fitness_portal::manage_nginx {
    service { 'nginx':
      ensure     => running,
      enable     => true,
      hasstatus  => true,
      hasrestart => true,
    }
  }
}
