# ==============================================================================
# Puppet Class: fitness_portal::install
# Manages system prerequisites, user accounts and directory structures
# ==============================================================================

class fitness_portal::install {
  $app_user    = $fitness_portal::app_user
  $app_group   = $fitness_portal::app_group
  $install_dir = $fitness_portal::install_dir
  $log_dir     = $fitness_portal::log_dir

  # 1. Install Java 21 Runtime Environment
  if $fitness_portal::manage_java {
    case $facts['os']['family'] {
      'Debian': {
        package { 'openjdk-21-jre-headless':
          ensure => installed,
        }
      }
      'RedHat': {
        package { 'java-21-openjdk-headless':
          ensure => installed,
        }
      }
      default: {
        package { 'openjdk-21-jre':
          ensure => installed,
        }
      }
    }
  }

  # 2. System Utilities
  $common_packages = ['curl', 'wget', 'tar']
  package { $common_packages:
    ensure => installed,
  }

  # 3. Optional Nginx Reverse Proxy
  if $fitness_portal::manage_nginx {
    package { 'nginx':
      ensure => installed,
    }
  }

  # 4. Service User and Group
  group { $app_group:
    ensure => present,
    system => true,
  }

  user { $app_user:
    ensure     => present,
    gid        => $app_group,
    system     => true,
    home       => $install_dir,
    shell      => '/bin/false',
    managehome => false,
    require    => Group[$app_group],
  }

  # 5. Directory Structure
  file { [$install_dir, "${install_dir}/bin", "${install_dir}/conf", $log_dir]:
    ensure  => directory,
    owner   => $app_user,
    group   => $app_group,
    mode    => '0750',
    require => [User[$app_user], Group[$app_group]],
  }
}
