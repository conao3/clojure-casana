{
  description = "clojure-casana";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-parts.url = "github:hercules-ci/flake-parts";
    treefmt-nix.url = "github:numtide/treefmt-nix";
  };

  outputs = inputs:
    inputs.flake-parts.lib.mkFlake {inherit inputs;} {
      imports = [
        inputs.treefmt-nix.flakeModule
      ];

      systems = ["x86_64-linux" "aarch64-darwin"];

      perSystem = {pkgs, ...}: let
        jdk = pkgs.graalvmPackages.graalvm-ce;
        clojure = pkgs.clojure.override {inherit jdk;};
      in {
        treefmt = {
          projectRootFile = "flake.nix";
          programs.alejandra.enable = true;
        };

        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            jdk
            clojure
            cljstyle
            clj-kondo
          ];
        };
      };
    };
}
