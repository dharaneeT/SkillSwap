import { Component } from "react";

export default class ErrorBoundary extends Component {
  state = { error: null };

  static getDerivedStateFromError(error) {
    return { error };
  }

  componentDidCatch(error, info) {
    console.error("UI crash:", error, info.componentStack);
  }

  render() {
    if (!this.state.error) return this.props.children;
    return (
      <pre
        style={{
          padding: 16,
          color: "#fecaca",
          background: "#0f172a",
          minHeight: "100vh",
          whiteSpace: "pre-wrap",
        }}
      >
        {String(this.state.error?.stack || this.state.error)}
      </pre>
    );
  }
}
