# ==============================================================================
# Puppet Class: fitness_portal::config
# Manages systemd unit file, application properties and reverse proxy
# ==============================================================================

class fitness_portal::config {
  $app_version   = $fitness_portal::app_version
  $app_port      = $fitness_portal::app_port
  $app_user      = $fitness_portal::app_user
  $app_group     = $fitness_portal::app_group
  $install_dir   = $fitness_portal::install_dir
  $log_dir       = $fitness_portal::log_dir
  $environment   = $fitness_portal::environment

  # 1. Spring Boot application configuration file
  file { "${install_dir}/conf/application.properties":
    ensure  => file,
    owner   => $app_user,
    group   => $app_group,
    mode    => '0640',
    content => template('fitness_portal/application.properties.erb'),
  }

  # 2. Systemd Service Unit File
  file { '/etc/systemd/system/fitness-portal.service':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => template('fitness_portal/fitness-portal.service.erb'),
    notify  => Exec['systemctl-daemon-reload-fitness'],
  }

  exec { 'systemctl-daemon-reload-fitness':
    command     => '/bin/systemctl daemon-reload',
    refreshonly => true,
  }

  # 3. Optional Nginx reverse proxy configuration
  if $fitness_portal::manage_nginx {
    file { '/etc/nginx/conf.d/fitness_portal.conf':
      ensure  => file,
      owner   => 'root',
      group   => 'root',
      mode    => '0644',
      source  => 'puppet:///modules/fitness_portal/nginx_reverse_proxy.conf',
      notify  => Service['nginx'],
      require => Package['nginx'],
    }
  }
}
