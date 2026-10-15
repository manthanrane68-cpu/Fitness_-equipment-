# ==============================================================================
# Puppet Site Manifest for Fitness Equipment Maintenance Portal Node
# ==============================================================================

node default {
  # Include core fitness portal class with default parameters
  class { 'fitness_portal':
    app_version   => '1.0.0',
    app_port      => 8080,
    manage_nginx  => true,
    manage_java   => true,
    environment   => 'production',
  }
}
