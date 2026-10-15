# ==============================================================================
# Puppet Class: fitness_portal
# Main orchestrator for install, config and service classes
# ==============================================================================

class fitness_portal (
  String  $app_version   = '1.0.0',
  Integer $app_port      = 8080,
  String  $app_user      = 'fitnessapp',
  String  $app_group     = 'fitnessapp',
  String  $install_dir   = '/opt/fitness-portal',
  String  $log_dir       = '/var/log/fitness-portal',
  Boolean $manage_nginx  = true,
  Boolean $manage_java   = true,
  String  $environment   = 'production',
) {

  # Enforce containment and execution ordering
  contain fitness_portal::install
  contain fitness_portal::config
  contain fitness_portal::service

  Class['fitness_portal::install']
  -> Class['fitness_portal::config']
  ~> Class['fitness_portal::service']
}
