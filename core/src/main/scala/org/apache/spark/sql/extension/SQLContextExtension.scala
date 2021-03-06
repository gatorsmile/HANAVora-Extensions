package org.apache.spark.sql.extension

import org.apache.spark.sql._
import org.apache.spark.sql.catalyst.ParserDialect
import org.apache.spark.sql.catalyst.analysis.{Analyzer, FunctionRegistry}
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.execution.datasources.DDLParser
import org.apache.spark.sql.extension.OptimizerFactory.ExtendableOptimizerBatch

/**
 * An extension for a [[SQLContext]]. This is to be used in combination
 * with [[ExtendableSQLContext]].
 *
 * @since 1.1
 */
private[sql] trait SQLContextExtension {

  /**
   * Additional check rules on a plan.
   *
   * @param analyzer The analyzer
   * @return A sequence with additional check rules
   */
  protected def extendedCheckRules(analyzer: Analyzer): Seq[LogicalPlan => Unit]

  /**
   * Additional resolution rules for the [[Analyzer]].
   *
   * @param analyzer The analyzer.
   * @return A list with additional resolution rules.
   */
  protected def resolutionRules(analyzer: Analyzer): List[Rule[LogicalPlan]]

  /**
   * Additional early batches for the
   * [[org.apache.spark.sql.catalyst.optimizer.Optimizer]].
   *
   * @return A list of optimization rules.
   */
  protected def optimizerEarlyBatches: List[ExtendableOptimizerBatch]

  /**
    * Additional expression simplification rules
    * [[org.apache.spark.sql.catalyst.optimizer.Optimizer]].
    *
    * @return A list of optimization rules.
    */
  protected def optimizerMainBatchRules: List[Rule[LogicalPlan]]

  protected def optimizerPostBatches: List[ExtendableOptimizerBatch]

  /**
   * Additional planning strategies.
   *
   * @param planner An [[ExtendedPlanner]] that a strategy might use to delegate a subtree.
   * @return A list of planning strategies.
   */
  protected def strategies(planner: ExtendedPlanner): List[Strategy]

  /**
   * A [[ParserDialect]] providing support for a SQL dialect.
   */
  protected def extendedParserDialect: ParserDialect

  /**
   * Provides a [[org.apache.spark.sql.execution.datasources.DDLParser]].
   *
   * @param parser Fallback parser.
   * @return
   */
  protected def extendedDdlParser(parser: String => LogicalPlan): DDLParser

  /**
   * Runs anyh code on the [[FunctionRegistry]]. This is useful
   * to register custom functions.
   *
   * @param registry The [[FunctionRegistry]]
   */
  protected def registerFunctions(registry: FunctionRegistry): Unit

}
